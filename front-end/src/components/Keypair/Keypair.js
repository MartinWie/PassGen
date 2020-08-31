import React, {useState, useEffect} from 'react';
import {Button, TextField} from '@material-ui/core/';
import RefreshIcon from '@material-ui/icons/Refresh';
import CloudDownloadIcon from '@material-ui/icons/CloudDownload';
import VisibilityIcon from '@material-ui/icons/Visibility';
import AssignmentIcon from '@material-ui/icons/Assignment';
import {makeStyles} from '@material-ui/core/styles'
import theme from '../../config/theme';
import './Keypair.css'

const useStyles = makeStyles({
    notchedOutline: {borderColor: theme.palette.primary.main + " !important"},
});

function Keypair(){

    const classes = useStyles();
    const [nameState,setNameState] = useState("key")

    return <div className="toolsframe">
      <div className="key-attributes">
        <TextField 
          fullWidth
          InputProps={{
            classes: {
              notchedOutline: classes.notchedOutline
            }
          }} 
          id="outlined-name" label="Name" 
          variant="outlined"
          onChange={(evt) => setNameState(evt.target.value)}
        />
        <Button variant="contained" color="primary" onClick={() => console.log(`Download keypair! ${nameState}`) }><CloudDownloadIcon /></Button>
        <Button variant="contained" color="primary" onClick={() => console.log(`Gen keypair! ${nameState}`) }> <RefreshIcon /></Button>
      </div>
        
      <div className="key-container">
        Public key:
        <TextField
          placeholder="Yeah! PassWords"
          multiline
          variant="outlined"
          rows={2}
          rowsMax={Infinity}
          fullWidth
          value=""
          InputProps={{
            readOnly: true,
            classes: {
              notchedOutline: classes.notchedOutline
            }
          }}
        />
      </div>

      <div className="key-container">
        Private key:
        <div className="private-key--block">
          <div className="private-key">
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
    </div>
}

export default Keypair
