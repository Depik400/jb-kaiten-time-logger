package com.depik400.kaitentimelogger.utils

object BranchParser {

    private val patterns = listOf(
        Regex("/([A-Z]+-(\\d+))(?:/|$)"),
        Regex("\\b([A-Z]+-(\\d+))\\b"),
        Regex("/([A-Z]+-(\\d+))_"),
        Regex("/([A-Z]+-(\\d+))(?:[/_]|$)"),
        Regex("\\b(\\d+)\\b") // просто число как запасной вариант
    )

    fun extractCardId(branchName: String?): Int? {
        if (branchName.isNullOrBlank()) return null

        for (pattern in patterns) {
            val matchResult = pattern.find(branchName)
            matchResult?.let {
                val groups = it.groupValues
                for (group in groups) {
                    val numbers = Regex("\\d+").find(group)
                    numbers?.let { numMatch ->
                        return numMatch.value.toIntOrNull()
                    }
                }
            }
        }

        return null
    }

    fun extractFullCardId(branchName: String?): String? {
        if (branchName.isNullOrBlank()) return null

        val patternsWithPrefix = listOf(
            Regex("/([A-Z]+-\\d+)"),
            Regex("\\b([A-Z]+-\\d+)\\b"),
            Regex("/([A-Z]+-\\d+)_")
        )

        for (pattern in patternsWithPrefix) {
            val match = pattern.find(branchName)
            match?.let {
                return it.groupValues[1]
            }
        }

        return null
    }
}