import React from 'react'

function CheckboxContainer(props) {
    const checkboxArray = Array.from(props.items)
    const checkboxItems = checkboxArray.map(item => <li>{item}</li>);

    return <div>
        {checkboxItems}
    </div>
    
}

export default CheckboxContainer