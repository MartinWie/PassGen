const randomWords = (length, checkboxState, seperator) => {
    let result = ''

    switch(true){
        case (checkboxState.checkedGer && checkboxState.checkedEng):
            for(var i = 0;i<length; i++){
                result= result + getGermanOrEnglishWord()
            }

        case (!(checkboxState.checkedGer) && checkboxState.checkedEng):
            for(var i = 0;i<length; i++){
                result= result + getEnglishWord()
            }

        case (checkboxState.checkedGer && !(checkboxState.checkedEng)):
            for(var i = 0;i<length; i++){
                result= result + getGermanWord()
            }
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