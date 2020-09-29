import React from 'react'

function CheckboxContainer(props) {
    const checkboxItems = props.items.map(item => <li>{item}</li>);

    return <div>
        {checkboxItems}
    </div>
    
}

export default CheckboxContainer