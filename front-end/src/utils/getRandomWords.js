const randomWords = (length, checkboxState, seperator) => {
    let result = ''
    let getWord

    if(checkboxState.German.value && checkboxState.English.value){
        getWord = getGermanOrEnglishWord
    } else if(!(checkboxState.German.value) && checkboxState.English.value){
        getWord = getEnglishWord
    } else if(checkboxState.German.value && !(checkboxState.English.value)){
        getWord = getGermanWord
    } else {
        return ("No language selected, feel free to choose ;)")
    }

    result= getWord()
    for(var i = 1;i<length; i++){
        result= result+ seperator + getWord()
    }

    return result
}


function getRandomZeroOrBigger() {
    const result = Math.floor(Math.random() * 2)
    return result
}

function getGermanWord() {
    return "German"
}

function getEnglishWord() {
    return "English"
}

function getGermanOrEnglishWord() {
    if (getRandomZeroOrBigger() == 0) {
        return getGermanWord()
    }
    return getEnglishWord()
}

module.exports = randomWords