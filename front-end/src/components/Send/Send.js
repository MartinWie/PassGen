import React, {useState} from 'react'
import './Send.css'
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';


function Send() {
    const [radioState, setRadioState] = useState("password");

    const handleRadioChange = (event, newValue) => {
        setRadioState(event.target.value);
        console.log(event.target.value)
    };
    
    
    return <div className="toolsframe">
        <div className="send-radio">
            <FormControl component="fieldset">
                <RadioGroup row aria-label="position" name="position" defaultValue={radioState}>
                    <FormControlLabel
                    value="password"
                    control={<Radio color="primary" />}
                    label="Password"
                    labelPlacement="top"
                    onChange={handleRadioChange}
                    />
                    <FormControlLabel
                    value="key"
                    control={<Radio color="primary" />}
                    label="Key"
                    labelPlacement="top"
                    onChange={handleRadioChange}
                    />
                </RadioGroup>
            </FormControl>
        </div>
        {getSendUI()}
    </div>

    function getSendUI() {
        if(radioState == "password") {
            return "PW UI"
        } else {
            return "key UI"
        }
    }

}
// implement send key/pw UI
export default Send;