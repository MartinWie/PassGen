import React from 'react'
import {Checkbox, FormControlLabel} from '@material-ui/core/';

function CheckboxContainer(props) {
    
    const checkboxes = createCheckboxObjectsFromParentState(props.items)

    return (<div>
        {checkboxes}
    </div>);

    function createCheckboxObjectsFromParentState(checkboxObject) {
        const checkboxes = []
        for(const [key, value] of Object.entries(checkboxObject)){
            checkboxes.push(
                <FormControlLabel
                    control={
                        <Checkbox
                            checked={value}
                            onChange={props.handleChangeCheckbox}
                            id={key}
                            color="primary"
                        />
                    }
                    label={key}
                />
            )
        }

        return checkboxes

    }
}

export default CheckboxContainer