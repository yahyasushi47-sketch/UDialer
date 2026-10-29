package com.example.util

import com.example.data.model.Contact

object T9SearchEngine {

    private val CHAR_TO_DIGIT_MAP = mapOf(
        'a' to '2', 'b' to '2', 'c' to '2',
        'd' to '3', 'e' to '3', 'f' to '3',
        'g' to '4', 'h' to '4', 'i' to '4',
        'j' to '5', 'k' to '5', 'l' to '5',
        'm' to '6', 'n' to '6', 'o' to '6',
        'p' to '7', 'q' to '7', 'r' to '7', 's' to '7',
        't' to '8', 'u' to '8', 'v' to '8',
        'w' to '9', 'x' to '9', 'y' to '9', 'z' to '9'
    )

    fun nameToT9(name: String): String {
        return buildString {
            for (ch in name.lowercase()) {
                val digit = CHAR_TO_DIGIT_MAP[ch]
                if (digit != null) {
                    append(digit)
                } else if (ch.isWhitespace()) {
                    append(' ')
                }
            }
        }
    }

    private fun normalizePhone(phone: String): String {
        return phone.filter { it.isDigit() }
    }

    data class T9MatchResult(
        val contact: Contact,
        val matchType: MatchType,
        val matchedSubtext: String = ""
    )

    enum class MatchType {
        NAME_T9,
        PHONE_NUMBER
    }

    /**
     * Filters and ranks contacts given a query of dialed digits (e.g. "666" for "Mom").
     */
    fun match(contacts: List<Contact>, query: String): List<T9MatchResult> {
        val cleanQuery = query.filter { it.isDigit() }
        if (cleanQuery.isEmpty()) return emptyList()

        val results = mutableListOf<T9MatchResult>()

        for (contact in contacts) {
            val normalizedPhone = normalizePhone(contact.phone)
            val nameT9 = nameToT9(contact.name)

            // Check if phone contains dialed query
            val phoneIndex = normalizedPhone.indexOf(cleanQuery)
            if (phoneIndex >= 0) {
                results.add(
                    T9MatchResult(
                        contact = contact,
                        matchType = MatchType.PHONE_NUMBER,
                        matchedSubtext = contact.phone
                    )
                )
                continue
            }

            // Check if name T9 contains query, or word initials
            val wordsT9 = nameT9.split(' ').filter { it.isNotBlank() }
            val directNameIndex = nameT9.replace(" ", "").indexOf(cleanQuery)
            val wordStartsWithQuery = wordsT9.any { it.startsWith(cleanQuery) }

            if (directNameIndex >= 0 || wordStartsWithQuery) {
                results.add(
                    T9MatchResult(
                        contact = contact,
                        matchType = MatchType.NAME_T9,
                        matchedSubtext = contact.name
                    )
                )
            }
        }

        return results
    }
}
