import React, {useState} from 'react'
import { useForm } from "react-hook-form";
import {Button, TextField, Checkbox, FormControlLabel, Slider,FormLabel} from '@material-ui/core/';
import RefreshIcon from '@material-ui/icons/Refresh';
import {makeStyles} from '@material-ui/core/styles'

const {register, handleSubmit, errors} = useForm;
const useStyles = makeStyles({
  notchedOutline: {borderColor: "yellow !important"},
});

function valuetext(value) {
  return {value};
  
}
function Words() {
  const [checkboxState, setCheckboxState] = useState({
    checkedGer: true,
    checkedEng: true
  });
  const classes = useStyles();
  
  return <div className="toolsframe">
      
      <span>Number of words:</span>
      <div className="">        
        <Slider
          defaultValue={4}
          getAriaValueText={valuetext}
          aria-labelledby="discrete-slider-small-steps"
          step={1}
          marks
          min={1}
          max={8}
          valueLabelDisplay="auto"
        />
        <FormLabel>4</FormLabel>
        <FormControlLabel
          control={
            <Checkbox
              checked={checkboxState.checkedGer}
              onChange={handleChangeCheckbox}
              name="checkedGer"
              color="primary"
            />
          }
          label="German"
        />
        <FormControlLabel
          control={
            <Checkbox
              checked={checkboxState.checkedEng}
              onChange={handleChangeCheckbox}
              name="checkedEng"
              color="primary"
            />
          }
          label="English"
        />
        <TextField 
          InputProps={{
            classes: {
              notchedOutline: classes.notchedOutline
            }
          }} 
          id="outlined-separator" label="Separator" 
          variant="outlined"
        />
        <Button variant="contained" color="primary"> <RefreshIcon /></Button>
      </div>
      <div>
        <TextField
          placeholder="Yeah! PassWords"
          multiline
          rows={3}
          rowsMax={Infinity}
          fullWidth
        />
      </div>
  </div>

// get theming right(color Both Textfields and button shape), than layout :)

//Debugg here: https://stackblitz.com/edit/material-ui-custom-outline-color?file=ValidField.js



function handleChangeCheckbox(evt){
  //Using he Object spread because setCheckboxState requires a new object for rerendering(Object spread helps us to create a new Object instead of copying the reference!)
  let tmp_CheckboxState = {...checkboxState}
  tmp_CheckboxState[evt.target.id] = !tmp_CheckboxState[evt.target.id]

  setCheckboxState(tmp_CheckboxState)
}

//found more elegant solution(replace here and in pw gen):

/*
  const handleChange = (event) => {
    setState({ ...state, [event.target.name]: event.target.checked });
  };
*/




}

export default Words;