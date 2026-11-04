package com.example.graduationproject.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarCatalogTest {
    @Test
    fun catalog_hasStableUniqueIdsAndEverySupportedCategory() {
        val ids = AvatarCatalog.options.map { it.id }

        assertEquals(ids.size, ids.toSet().size)
        assertEquals(16, AvatarCatalog.options.size)
        assertTrue(AvatarCatalog.options.any { it.category == AvatarCategory.PERSON })
        assertTrue(AvatarCatalog.options.any { it.category == AvatarCategory.ANIMAL })
        assertTrue(AvatarCatalog.options.any { it.category == AvatarCategory.LIFE })
        assertTrue(AvatarCatalog.options.any { it.category == AvatarCategory.NATURE })
        assertFalse(ids.any(String::isBlank))
    }

    @Test
    fun unknownAvatarAndColor_fallBackToFixedDefaults() {
        assertEquals(DEFAULT_PROFILE_AVATAR_ID, AvatarCatalog.find("missing").id)
        assertEquals(
            DEFAULT_PROFILE_AVATAR_COLOR_ID,
            AvatarCatalog.findColor("missing").id
        )
    }
}
