import React, {useState} from 'react'
import { useForm } from "react-hook-form";
import { Slider,FormLabel } from '@material-ui/core';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import RefreshIcon from '@material-ui/icons/Refresh';

const {register, handleSubmit, errors} = useForm;


function valuetext(value) {
  return {value};
  
}
function Words() {
  const [checkboxState, setCheckboxState] = useState({
    checkedGer: true,
    checkedEng: true
  });
  
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
        <TextField id="outlined-separator" label="Separator" variant="outlined" />
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