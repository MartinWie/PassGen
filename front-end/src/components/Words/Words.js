import React, {useState, useEffect} from 'react'
import {Button, TextField, Checkbox, FormControlLabel, Slider} from '@material-ui/core/';
import RefreshIcon from '@material-ui/icons/Refresh';
import {makeStyles} from '@material-ui/core/styles'
import theme from '../../config/theme';
import './Words.css'
import {isMobile} from "react-device-detect";
const randomWords = require('../../utils/getRandomWords')

const useStyles = makeStyles({
  notchedOutline: {borderColor: theme.palette.primary.main + " !important"},
});

function Words() {

  const [checkboxState, setCheckboxState] = useState({
    checkedGer: true,
    checkedEng: true
  });

  const [sliderState, setSliderState] = useState(4)
  const [seperatorState, setSeperatorState] = useState("-")
  const [outputState, setOutputState] = useState("")

  useEffect(() => {
    setOutputState(randomWords(sliderState,checkboxState,seperatorState))

  },[checkboxState,sliderState,seperatorState])

  const classes = useStyles();

  return <div className="toolsframe">
      
      <span>Number of words:</span>
      <div style={wordsInputStylingPerDevice()}>        
        <Slider
          defaultValue={4}
          getAriaValueText={valuetext}
          aria-labelledby="discrete-slider-small-steps"
          step={1}
          marks
          min={1}
          max={8}
          valueLabelDisplay="auto"
          onChange={(evt, value) => setSliderState(value)}
        />
      </div>
      <div style={wordsInputStylingPerDevice()}>
        <div className="wordsConfigCheckboxes">
          <FormControlLabel
            control={
              <Checkbox
                checked={checkboxState.checkedGer}
                onChange={handleChangeCheckbox}
                id="checkedGer"
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
                id="checkedEng"
                color="primary"
              />
            }
            label="English"
          />
        </div>
        <TextField 
          InputProps={{
            classes: {
              notchedOutline: classes.notchedOutline
            }
          }} 
          id="outlined-separator" label="Separator" 
          variant="outlined"
          onChange={(evt) => setSeperatorState(evt.target.value)}
        />
        <Button variant="contained" color="primary" onClick={
            () => setOutputState(randomWords(sliderState,checkboxState,seperatorState)) 
          }> <RefreshIcon /></Button>
      </div>
      <div className="wordsOutput">
        <TextField
          placeholder="Yeah! PassWords"
          multiline
          variant="outlined"
          rows={2}
          rowsMax={Infinity}
          fullWidth
          value={outputState}
          InputProps={{
            readOnly: true,
            classes: {
              notchedOutline: classes.notchedOutline
            }
          }}
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

  function wordsInputStylingPerDevice(){
    var style = {
      display: 'flex',
      justifyContent: 'space-around'
    }
    if(isMobile){
      style.flexDirection = 'column';
    }
    return style;
  }

  function valuetext(value) {
    return {value};
    
  }

}

export default Words;