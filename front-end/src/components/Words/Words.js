import React, {useState, useEffect} from 'react'
import SliderContainer from '../../hooks/SliderContainer'
import CheckboxContainer from '../../hooks/CheckboxContainer'
import PassContainer from '../../hooks/PassContainer'

const randomWords = require('../../utils/getRandomWords')

function Words() {

  const [checkboxState, setCheckboxState] = useState({
    German: {
      value: true,
      label: "German"
    },
    English: {
      value: true,
      label: "English"
    }
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
      
      <SliderContainer min={1} 
        max={8} 
        defaultValue={4} 
        setSliderValue={setSliderState} 
        sliderValue={sliderState} 
        redoButtonFunction={regenPassword}
        setSeperatorState={setSeperatorState}
      />

      <CheckboxContainer items={checkboxState} handleChangeCheckbox={helperSetCheckboxState} /> 

      <PassContainer title="Password:" value={outputState} />
  
  </div>

function helperSetCheckboxState(state){
  setCheckboxState(state)
}

}

export default Words;