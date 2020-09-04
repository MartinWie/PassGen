import React, {useState} from 'react'
import VisibilityIcon from '@material-ui/icons/Visibility';
import AssignmentIcon from '@material-ui/icons/Assignment';
import {Button, TextField} from '@material-ui/core/';
import {makeStyles} from '@material-ui/core/styles'
import Tooltip from "@material-ui/core/Tooltip";
import './PassContainer.css'
import theme from '../config/theme';

const useStyles = makeStyles({
    notchedOutline: {borderColor: theme.palette.primary.main + " !important"},
});

function PassContainer(props) {
    const classes = useStyles();
    const [nameState,setNameState] = useState("key")
    const [inputState,setInputState] = useState(props.value)
    const [passwordHiddenState,setPasswordHiddenState] = useState(props.hidden)

    return(
        <div className="passcontainer">
        <span>{props.title}</span>
        <div className="passcontainer--block">
          <div className="passcontainer-text">
            <TextField 
              placeholder="Yeah! PassWords"
              variant="outlined"
              rows={2}
              fullWidth
              rowsMax={Infinity}
              value={inputState}
              type={passwordHiddenState ? 'password' : 'text'}
              InputProps={{
                readOnly: true,
                classes: {
                  notchedOutline: classes.notchedOutline
                }
              }}
            />
          </div>
          <Button id="passcontainerShowButton" variant="contained" color="primary" onClick={() => setPasswordHiddenState(!passwordHiddenState) }> <VisibilityIcon /></Button>
          <Tooltip title="Copy to clipboard" placement="top">
            <Button variant="contained" color="primary" onClick={() => navigator.clipboard.writeText(inputState) }> <AssignmentIcon /></Button>
          </Tooltip>
        </div>
      </div>
    )   
}
//Next step add clipboard functionality then get props from parent(Heading, pwhiddenornot, value)
export default PassContainer