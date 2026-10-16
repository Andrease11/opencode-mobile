package io.github.beppe2880.opencodemobile.core.network.mapping

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import io.github.beppe2880.opencodemobile.api.models.ModelRef as ApiModelRef
import io.github.beppe2880.opencodemobile.core.domain.ModelRef as DomainModelRef

class ModelRefMappingTest {

    @Test
    fun `the server default variant is no variant, so sending it back omits the field`() {
        val domain = ApiModelRef(id = "haiku", providerID = "anthropic", variant = "default").toDomain()

        assertNull(domain.variant)
        assertNull(domain.toApi().variant)
        assertEquals("high", ApiModelRef(id = "haiku", providerID = "anthropic", variant = "high").toDomain().variant)
        assertEquals("low", DomainModelRef("haiku", "anthropic", "low").toApi().variant)
    }
}
