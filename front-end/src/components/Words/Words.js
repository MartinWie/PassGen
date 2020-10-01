import React, {useState, useEffect} from 'react'
import {Checkbox, FormControlLabel} from '@material-ui/core/';
import './Words.css'
import {isMobile} from "react-device-detect";
import SliderContainer from '../../hooks/SliderContainer'
import InputContainer from '../../hooks/InputContainer'
import CheckboxContainer from '../../hooks/CheckboxContainer'
import PassContainer from '../../hooks/PassContainer'

const randomWords = require('../../utils/getRandomWords')

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

  function regenPassword(){
    setOutputState(randomWords(sliderState,checkboxState,seperatorState))
  }

  return <div className="toolsframe">
      <h3>Generate a passWord!</h3>
      <span>Number of words:</span>
      
      <CheckboxContainer items={[]} />
      
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

      <PassContainer title="Password:" value={outputState} />

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

 // send component add title for public key
 // rewrite slidercontainer and build it something, make it more general, that the seperator also can fit in there
 // fix styling (create component for checkboxes and use here and in Passgen)
 // put seperator and checkboxes in a good order

export default Words;