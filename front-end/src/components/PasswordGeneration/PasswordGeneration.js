import React, {useState} from 'react';
import './PasswordGeneration.css';
import { FormControl, Button, InputGroup, Form } from 'react-bootstrap';
//import useForm from 'react-hook-form';

//had to coement that out because css overwrites button css (border radius figure remove of botstrap or overwite )
//import 'bootstrap/dist/css/bootstrap.min.css';
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

  return <div id="main">
      <h3>Enter a length for your password</h3>

      <InputGroup className="mb-3">
        <FormControl
          placeholder={pwLength}
          aria-label={pwLength}
          aria-describedby="basic-addon2"
          onChange={evt => handleChange(evt)}
        />
        <InputGroup.Append>
          <Button onClick={handleClick} variant="outline-secondary"><ArrowClockwise /></Button>
        </InputGroup.Append>
      </InputGroup>
      <div className="HorizontalLayout" >
        <Form.Check id="checkboxNumbers" onChange={handleChangeCheckbox} className="PWGenCheckbox" type="checkbox" label="Numbers" checked={checkboxState["checkboxNumbers"]} />
        <Form.Check id="checkboxSpecialChars" onChange={handleChangeCheckbox} className="PWGenCheckbox" type="checkbox" label="Special characters" checked={checkboxState["checkboxSpecialChars"]} />
        <Form.Check id="checkboxUpper" onChange={handleChangeCheckbox} className="PWGenCheckbox" type="checkbox" label="Upper case" checked={checkboxState["checkboxUpper"]} />
        <Form.Check id="checkboxLower" onChange={handleChangeCheckbox} className="PWGenCheckbox" type="checkbox" label="Lower case" checked={checkboxState["checkboxLower"]} />
      </div>
      <Form.Control as="textarea" rows="5" readOnly={true} value={password} />
    </div>;


  function handleChangeCheckbox(evt){
    //Using he Object spread because setCheckboxState requires a new object for rerendering(Object spread helps us to create a new Object instead of copying the reference!)
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