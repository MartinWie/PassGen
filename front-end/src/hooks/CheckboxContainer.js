import React from 'react'
import {Checkbox, FormControlLabel} from '@material-ui/core/';

function CheckboxContainer(props) {
    
    const checkboxes = createCheckboxObjectsFromParentState(props.items)

    return (<div style={wordsInputStylingPerDevice()}>
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

    function wordsInputStylingPerDevice(){
        var style = {
          display: 'flex',
          justifyContent: 'space-around',
          margin: "5px"
        }
        return style;
      }
}

export default CheckboxContainer