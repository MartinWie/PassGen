import React, {useState} from 'react'
import VisibilityIcon from '@material-ui/icons/Visibility';
import AssignmentIcon from '@material-ui/icons/Assignment';
import './PassContainer.css'
import {makeStyles} from '@material-ui/core/styles'
import theme from '../config/theme';
import {Button, TextField} from '@material-ui/core/';

const useStyles = makeStyles({
    notchedOutline: {borderColor: theme.palette.primary.main + " !important"},
});

function PassContainer() {
    const classes = useStyles();
    const [nameState,setNameState] = useState("key")

    return(
        <div className="passcontainer">
        Private key:
        <div className="passcontainer--block">
          <div className="passcontainer-text">
            <TextField
              placeholder="Yeah! PassWords"
              multiline
              variant="outlined"
              rows={2}
              fullWidth
              rowsMax={Infinity}
              value=""
              InputProps={{
                readOnly: true,
                classes: {
                  notchedOutline: classes.notchedOutline
                }
              }}
            />
          </div>
          <Button variant="contained" color="primary" onClick={() => console.log(`Show private key! ${nameState}`) }> <VisibilityIcon /></Button>
          <Button variant="contained" color="primary" onClick={() => console.log(`copy private key! ${nameState}`) }> <AssignmentIcon /></Button>
        </div>
      </div>
    )   
}

export default PassContainer