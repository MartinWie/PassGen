const randomString = (length,checkboxState) => {
    let result = ''
    const numbers = '0123456789'
    const specialChars = '!\"\§\$\%\&\(\)'
    const upperChars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
    const lowerChars = 'abcdefghijklmnopqrstuvwxyz'
    let characters = ''

    if(checkboxState.checkboxNumbers.value) characters += numbers
    if(checkboxState.checkboxSpecialChars.value) characters += specialChars
    if(checkboxState.checkboxUpper.value) characters += upperChars
    if(checkboxState.checkboxLower.value) characters += lowerChars

    const charactersLength = characters.length;
    for ( var i = 0; i < length; i++ ) {
      result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
  
    return result
}

module.exports = randomString
