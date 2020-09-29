import React, {useState, useEffect} from 'react'
import {TextField, Checkbox, FormControlLabel} from '@material-ui/core/';
import {makeStyles} from '@material-ui/core/styles'
import theme from '../../config/theme';
import './Words.css'
import {isMobile} from "react-device-detect";
import SliderContainer from '../../hooks/SliderContainer'
import InputContainer from '../../hooks/InputContainer'
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

  function regenPassword(){
    setOutputState(randomWords(sliderState,checkboxState,seperatorState))
  }

  return <div className="toolsframe">
      
      <span>Number of words:</span>
      <SliderContainer min={1} 
        max={8} 
        defaultValue={4} 
        setSliderValue={setSliderState} 
        sliderValue={sliderState} 
        redoButtonFunction={regenPassword}
      />


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
      </div>

      <InputContainer outlinedName="Separator" 
        inputChange={setSeperatorState} 
        hideRefreshbutton hideDownloadbutton
      />
      
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

 // fix styling (create component for checkboxes and use here and in Passgen)

export default Words;