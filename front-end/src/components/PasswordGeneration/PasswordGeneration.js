import React, {useState} from 'react';
import './PasswordGeneration.css';
import { FormControl, Button, InputGroup, Form } from 'react-bootstrap';
import { ArrowClockwise } from 'react-bootstrap-icons';
const randomString = require('../../utils/getRandomString')

function PasswordGeneration() {

  const [password,setPassword] = useState("Choose a length!");
  const [pwLength,setPwLength] = useState("length");
  const [checkboxState, setCheckboxState] = useState({
    checkboxNumbers: true,
    checkboxSpecialChars: true,
    checkboxUpper: true,
    checkboxLower:true
  });

  return <div className="toolsframe">
      <h3>Enter a length for your password</h3>

      <div className="HorizontalLayout">
        <FormControl className="Fullwidth"
          placeholder={pwLength}
          aria-label={pwLength}
          aria-describedby="basic-addon2"
          onChange={evt => handleChange(evt)}
        />
        <InputGroup.Append>
          <Button onClick={handleClick} variant="outline-secondary"><ArrowClockwise className="ButtonImage" /></Button>
        </InputGroup.Append>
      </div>
      <div className="HorizontalLayout" >
        <Form.Check id="checkboxNumbers" onChange={handleChangeCheckbox} className="PWGenCheckbox" type="checkbox" label=" 0-9" checked={checkboxState.checkboxNumbers} />
        <Form.Check id="checkboxSpecialChars" onChange={handleChangeCheckbox} className="PWGenCheckbox" type="checkbox" label=" !/%..." checked={checkboxState.checkboxSpecialChars} />
        <Form.Check id="checkboxUpper" onChange={handleChangeCheckbox} className="PWGenCheckbox" type="checkbox" label=" A-Z" checked={checkboxState.checkboxUpper} />
        <Form.Check id="checkboxLower" onChange={handleChangeCheckbox} className="PWGenCheckbox" type="checkbox" label=" a-z" checked={checkboxState.checkboxLower} />
      </div>
      <div className="HorizontalLayout">
        <Form.Control className="Fullwidth" as="textarea" rows="8" readOnly={true} value={password} />
      </div>
    </div>;


  function handleChangeCheckbox(evt){
    //Using the Object spread because setCheckboxState requires a new object for rerendering(Object spread helps us to create a new Object instead of copying the reference!)
    let tmp_CheckboxState = {...checkboxState}
    tmp_CheckboxState[evt.target.id] = !tmp_CheckboxState[evt.target.id]

    setCheckboxState(tmp_CheckboxState)
  }

  function handleClick() {
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