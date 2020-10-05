import React from 'react'

function CheckboxContainer(props) {
    
    const entriesMap = getMapfromCheckboxObject(props.items)

    console.log(entriesMap)

    return (<div>

    </div>);

    function getMapfromCheckboxObject(checkboxObject) {
        const checkboxMap = new Map();
        for(const [key, value] of Object.entries(checkboxObject)){
            checkboxMap.set(key,value)
        }

        return checkboxMap

    }
}

export default CheckboxContainer