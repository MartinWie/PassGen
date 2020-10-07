import React, {useState, useEffect} from 'react';
import './PasswordGeneration.css';
import PassContainer from '../../hooks/PassContainer'
import SliderContainer from '../../hooks/SliderContainer'
import CheckboxContainer from '../../hooks/CheckboxContainer'

const randomString = require('../../utils/getRandomString')


function PasswordGeneration() {

  const [password,setPassword] = useState("Choose a length!");
  const [pwLength,setPwLength] = useState(42);
  const [checkboxState, setCheckboxState] = useState({
    checkboxNumbers: {
      value: true,
      label: "0-9"
    },
    checkboxSpecialChars: {
      value: true,
      label: "!/%..."
    },
    checkboxUpper: {
      value: true,
      label: "A-Z"
    },
    checkboxLower:{
      value: true,
      label: "a-z"
    }
  });
  useEffect(() => {
    regenPassword()
  },[pwLength,checkboxState]);

  return (
    <div className="toolsframe">
      <h3>Classical password</h3>

      <SliderContainer min={1} 
        max={62} 
        defaultValue={42} 
        setSliderValue={setPwLength} 
        sliderValue={pwLength} 
        redoButtonFunction={regenPassword}
        hideSeperatorInput
      />

      <CheckboxContainer items={checkboxState} handleChangeCheckbox={helperSetCheckboxState} />

      <PassContainer title="Password:" hidden value={password} />
    </div>
  );
  
  function helperSetCheckboxState(state){
    setCheckboxState(state)
  }

  function regenPassword() {
    setPassword(randomString(pwLength,checkboxState))
  }

  function handleChange(evt){
    //saving tmp_pwLength in tmp const because setState is async
    const tmp_pwLength = parseInt(evt.target.value, 10)
    if(Number.isInteger(tmp_pwLength)){
      setPwLength(tmp_pwLength)
      setPassword(randomString(tmp_pwLength,checkboxState))
    } else {
      setPwLength("Choose a length!")
      setPassword("Choose a length!")
    }
  }
}

export default PasswordGeneration;