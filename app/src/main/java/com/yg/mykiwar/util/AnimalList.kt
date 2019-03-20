package com.yg.mykiwar.util

object AnimalList {
    val animalList = arrayOf("큰뿔양", "버팔로", "낙타", "고양이", "소", "개",
            "코끼리", "퍼렛", "여우", "가젤", "염소", "말",
            "사자", "쥐", "수달", "팬더", "펭귄", "돼지",
            "라쿤", "양", "뱀")

    val animalListE = arrayOf("bighornsheep", "buffalo", "camel",
            "cat", "cow", "dog",
            "elephant", "ferret", "fox", "gazelle", "goat", "horse",
            "lion", "mouse", "riverotter", "panda", "penguin", "pig",
            "raccoon", "sheep", "snake")

    var animalMatch = HashMap<String, String>()

    fun getMatch() : HashMap<String, String>{
        for (animal in animalList)
            animalMatch[animal] = animalListE[animalList.indexOf(animal)]
        return animalMatch
    }

}