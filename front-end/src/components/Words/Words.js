import React, {useState} from 'react'
import { useForm } from "react-hook-form";
import {Button, TextField, Checkbox, FormControlLabel, Slider,FormLabel} from '@material-ui/core/';
import RefreshIcon from '@material-ui/icons/Refresh';
import {makeStyles} from '@material-ui/core/styles'
import theme from '../../config/theme';
import './Words.css'

const {register, handleSubmit, errors} = useForm;
const useStyles = makeStyles({
  notchedOutline: {borderColor: theme.palette.primary.main + " !important"},
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
      <div className="screenSizeContainer">
        <div className="wordsConfig">        
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
        </div>
        <div className="wordsConfig">
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
      </div>
      <div className="wordsOutput">
        <TextField
          placeholder="Yeah! PassWords"
          multiline
          variant="outlined"
          rows={2}
          rowsMax={Infinity}
          fullWidth
          InputProps={{
            readOnly: true,
            classes: {
              notchedOutline: classes.notchedOutline
            }
          }}
        />
      </div>
  </div>

// improve mobile ui , maybe grid and pülace seperator and botton under chekcboxes when scree to small
// afterwards get basic functionality in this class with helper class

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