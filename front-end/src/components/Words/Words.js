import React, {useState, useEffect} from 'react'
import SliderContainer from '../../hooks/SliderContainer'
import CheckboxContainer from '../../hooks/CheckboxContainer'
import PassContainer from '../../hooks/PassContainer'

const randomWords = require('../../utils/getRandomWords')

function Words() {

  const [checkboxState, setCheckboxState] = useState({
    German: true,
    English: true
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

      <CheckboxContainer items={checkboxState} handleChangeCheckbox={handleChangeCheckbox} /> 

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

}

export default Words;