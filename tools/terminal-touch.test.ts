import { expect, test } from "bun:test"
import { readFileSync } from "node:fs"
import { runInNewContext } from "node:vm"

const script = readFileSync(new URL("../android/feature/terminal/src/main/assets/terminal/terminal.js", import.meta.url), "utf8")

function terminal() {
  type Listener = (event: any) => void
  class Element {
    listeners = new Map<string, Listener[]>()
    height = 400
    addEventListener(type: string, listener: Listener) {
      const listeners = this.listeners.get(type) ?? []
      listeners.push(listener)
      this.listeners.set(type, listeners)
    }
    getBoundingClientRect() { return { height: this.height } }
    dispatch(type: string, touches: any[] = [], extra = {}) {
      const event = {
        touches, defaultPrevented: false, stopped: false,
        preventDefault() { this.defaultPrevented = true },
        stopImmediatePropagation() { this.stopped = true },
        ...extra,
      }
      for (const listener of this.listeners.get(type) ?? []) {
        listener(event)
        if (event.stopped) break
      }
      return event
    }
  }
  const screen = new Element()
  const root = Object.assign(new Element(), { querySelector: () => screen })
  const scrolls: number[] = []
  let focused = 0
  let selected = false
  let nativeSelection = ""
  const term = {
    element: root, rows: 20, cols: 80,
    buffer: { active: { type: "normal", baseY: 200 } },
    modes: { mouseTrackingMode: "none" },
    hasSelection: () => selected,
    scrollLines: (rows: number) => scrolls.push(rows),
    focus: () => { focused++ },
    loadAddon() {}, open() {}, onData() {}, onResize() {},
  }
  runInNewContext(script, {
    window: { OcTerminal: { font() { throw new Error("Use fallback font") }, resize() {}, ready() {} }, getSelection: () => nativeSelection },
    document: { getElementById: () => root },
    Terminal: function () { return term },
    FitAddon: { FitAddon: class { fit() {} } },
    ResizeObserver: class { observe() {} },
  })
  const touch = (x: number, y: number, id = 1) => ({ identifier: id, clientX: x, clientY: y })
  return {
    term, screen, scrolls, touch,
    select: () => { selected = true },
    selectNative: () => { nativeSelection = "selected text" },
    start: (x = 100, y = 100) => screen.dispatch("touchstart", [touch(x, y)]),
    move: (x: number, y: number) => screen.dispatch("touchmove", [touch(x, y)]),
    click: () => {
      const event = screen.dispatch("click", [], { pointerType: "touch" })
      if (!event.stopped) root.dispatch("click")
      return { ...event, focused }
    },
  }
}

test("vertical swipes scroll history in both directions", () => {
  const t = terminal()
  t.start()
  expect(t.move(100, 160).defaultPrevented).toBe(true)
  t.move(100, 120)
  expect(t.scrolls).toEqual([-3, 2])
})

test("small movements remain taps and still focus the terminal", () => {
  const t = terminal()
  t.start()
  expect(t.move(103, 105).defaultPrevented).toBe(false)
  t.screen.dispatch("touchend")
  expect(t.scrolls).toEqual([])
  expect(t.click().focused).toBe(1)
})

test("horizontal gestures are not captured even if they later move vertically", () => {
  const t = terminal()
  t.start()
  expect(t.move(125, 102).defaultPrevented).toBe(false)
  expect(t.move(125, 180).defaultPrevented).toBe(false)
  expect(t.scrolls).toEqual([])
})

test("slow movements accumulate fractional rows without losing distance", () => {
  const t = terminal()
  t.start()
  for (const y of [108, 112, 116, 120, 124, 128, 132, 136, 140]) t.move(100, y)
  expect(t.scrolls).toEqual([-1, -1])
})

test("row size follows the current viewport instead of a fixed pixel distance", () => {
  const t = terminal()
  t.screen.height = 200
  t.start()
  t.move(100, 140)
  expect(t.scrolls).toEqual([-4])
})

for (const type of ["touchend", "touchcancel"]) {
  test(`${type} ends the gesture`, () => {
    const t = terminal()
    t.start()
    t.screen.dispatch(type)
    expect(t.move(100, 180).defaultPrevented).toBe(false)
    expect(t.scrolls).toEqual([])
  })
}

test("multitouch cancels the gesture until a new touch starts", () => {
  const t = terminal()
  t.start()
  t.screen.dispatch("touchmove", [t.touch(100, 140), t.touch(200, 140, 2)])
  t.move(100, 180)
  expect(t.scrolls).toEqual([])
  t.start()
  t.move(100, 140)
  expect(t.scrolls).toEqual([-2])
})

test("a synthetic click after a swipe cannot focus the input", () => {
  const t = terminal()
  t.start()
  t.move(100, 160)
  t.screen.dispatch("touchend")
  expect(t.click()).toMatchObject({ defaultPrevented: true, stopped: true, focused: 0 })
  t.start()
  t.screen.dispatch("touchend")
  expect(t.click().focused).toBe(1)
})

for (const mode of ["alternate buffer", "no history", "mouse tracking", "xterm selection", "native selection"]) {
  test(`leaves ${mode} alone`, () => {
    const t = terminal()
    if (mode === "alternate buffer") t.term.buffer.active.type = "alternate"
    if (mode === "no history") t.term.buffer.active.baseY = 0
    if (mode === "mouse tracking") t.term.modes.mouseTrackingMode = "vt200"
    if (mode === "xterm selection") t.select()
    if (mode === "native selection") t.selectNative()
    t.start()
    expect(t.move(100, 180).defaultPrevented).toBe(false)
    expect(t.scrolls).toEqual([])
  })
}
