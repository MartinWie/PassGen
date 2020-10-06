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
                            checked={value.value}
                            onChange={handleChangeCheckbox}
                            id={key}
                            color="primary"
                        />
                    }
                    key={key}
                    label={value.label}
                />
            )
        }
        return checkboxes

    }

    function handleChangeCheckbox(evt){
        //Using he Object spread because setCheckboxState requires a new object for rerendering(Object spread helps us to create a new Object instead of copying the reference!)
        let tmp_checkboxes = {...props.items}
        tmp_checkboxes[evt.target.id].value = !tmp_checkboxes[evt.target.id].value
    
        props.handleChangeCheckbox(tmp_checkboxes)
      }
      //found more elegant solution(replace here and in pw gen):
      /*
        const handleChange = (event) => {
          setState({ ...state, [event.target.name]: event.target.checked });
        };
      */
    

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