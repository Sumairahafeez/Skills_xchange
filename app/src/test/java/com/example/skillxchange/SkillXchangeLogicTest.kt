package com.example.skillxchange

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for core business logic of SkillXchange.
 */
class SkillXchangeLogicTest {

    @Test
    fun testLikeToggleLogic() {
        val currentUserId = "user123"
        val likedBy = mutableListOf("user456", "user789")
        
        // Simulating "Like" (not in list)
        if (!likedBy.contains(currentUserId)) {
            likedBy.add(currentUserId)
        }
        assertTrue(likedBy.contains(currentUserId))
        assertEquals(3, likedBy.size)

        // Simulating "Unlike" (already in list)
        if (likedBy.contains(currentUserId)) {
            likedBy.remove(currentUserId)
        }
        assertTrue(!likedBy.contains(currentUserId))
        assertEquals(2, likedBy.size)
    }

    @Test
    fun testPostAuthorFallback() {
        val authDisplayName = "John Doe"
        val profileName: String? = null
        
        val finalName = profileName ?: authDisplayName ?: "Unknown"
        assertEquals("John Doe", finalName)
    }

    @Test
    fun testChatIdConsistency() {
        val uid1 = "abc"
        val uid2 = "xyz"
        
        // Logical check: consistency regardless of who is sender/receiver
        val chatId1 = if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
        val chatId2 = if (uid2 < uid1) "${uid2}_${uid1}" else "${uid1}_${uid2}"
        
        assertEquals(chatId1, chatId2)
        assertEquals("abc_xyz", chatId1)
    }

    @Test
    fun testTagSelection() {
        val selectedTags = mutableListOf<String>()
        val tagToAdd = "Kotlin"
        
        selectedTags.add(tagToAdd)
        assertTrue(selectedTags.contains("Kotlin"))
        
        selectedTags.remove(tagToAdd)
        assertTrue(selectedTags.isEmpty())
    }
}
