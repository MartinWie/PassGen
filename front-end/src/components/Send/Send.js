import React, {useState} from 'react'
import './Send.css'
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';


function Send() {
    const [radioState, setRadioState] = useState(0);

    const handleRadioChange = (event, newValue) => {
        setRadioState(event.target.value);
        console.log(event.target.value)
    };
    
    
    return <div className="toolsframe">
        <div>
            <FormControl component="fieldset">
                <RadioGroup row aria-label="position" name="position" defaultValue="password">
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
       
    </div>
}
// center the radio buttons  with flex and then create the ui for send
export default Send;