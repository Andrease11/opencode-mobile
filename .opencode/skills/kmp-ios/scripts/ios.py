#!/usr/bin/env python3
"""Tiny iOS simulator driver over DeviceKit JSON-RPC (127.0.0.1:12004).
usage: ios.py ui | tap LABEL [index] | tapxy X Y | text STR | swipe X1 Y1 X2 Y2 | shot NAME | key BUTTON"""
import json, sys, subprocess, urllib.request, time
def rpc(method, params=None):
    body = json.dumps({"jsonrpc": "2.0", "method": method, "params": params or {}, "id": 1}).encode()
    req = urllib.request.Request("http://127.0.0.1:12004/rpc", body, {"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, timeout=60))
def elements():
    out = []
    def walk(n):
        if isinstance(n, dict):
            if n.get("label") or n.get("value"):
                r = n.get("rect") or {}
                out.append((n.get("type", ""), n.get("label") or "", n.get("value") or "", r))
            for v in n.values():
                if isinstance(v, (dict, list)): walk(v)
        elif isinstance(n, list):
            for x in n: walk(x)
    walk(rpc("device.dump.ui").get("result"))
    return out
cmd = sys.argv[1]
if cmd == "ui":
    for t, l, v, r in elements():
        if t in ("XCUIElementTypeApplication",) or r.get("y", 0) < 50 and "battery" in l: continue
        print(f'{t.replace("XCUIElementType","")}\t{l[:70]!r}\t{v[:30]!r}\t{int(r.get("x",0)+r.get("width",0)/2)},{int(r.get("y",0)+r.get("height",0)/2)}')
elif cmd == "tap":
    label = sys.argv[2]; idx = int(sys.argv[3]) if len(sys.argv) > 3 else 0
    m = [e for e in elements() if e[1] == label] or [e for e in elements() if label.lower() in e[1].lower()]
    if len(m) <= idx: print("NOT FOUND", label); sys.exit(1)
    r = m[idx][3]; x = r["x"] + r["width"] / 2; y = r["y"] + r["height"] / 2
    rpc("device.io.tap", {"x": x, "y": y, "deviceId": "any"}); print("tapped", m[idx][1][:60], int(x), int(y))
elif cmd == "tapxy":
    rpc("device.io.tap", {"x": float(sys.argv[2]), "y": float(sys.argv[3]), "deviceId": "any"})
elif cmd == "longpress":
    label = sys.argv[2]
    m = [e for e in elements() if label.lower() in e[1].lower()]
    r = m[0][3]; rpc("device.io.longpress", {"x": r["x"] + r["width"]/2, "y": r["y"] + r["height"]/2, "duration": 1.0, "deviceId": "any"}); print("longpressed", m[0][1][:60])
elif cmd == "text":
    rpc("device.io.text", {"text": sys.argv[2], "deviceId": "any"})
elif cmd == "swipe":
    a = list(map(float, sys.argv[2:6]))
    rpc("device.io.swipe", {"x1": a[0], "y1": a[1], "x2": a[2], "y2": a[3], "duration": 0.4, "deviceId": "any"})
elif cmd == "key":
    rpc("device.io.button", {"button": sys.argv[2], "deviceId": "any"})
elif cmd == "shot":
    subprocess.run(["xcrun", "simctl", "io", "booted", "screenshot", "/tmp/s.png"], capture_output=True)
    subprocess.run(["sips", "-Z", "1000", "/tmp/s.png", "--out", f"/tmp/{sys.argv[2]}.png"], capture_output=True)
elif cmd == "wait":
    time.sleep(float(sys.argv[2]))
