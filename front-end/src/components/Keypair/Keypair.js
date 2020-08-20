import React, {useState, useEffect} from 'react';
import {Button, TextField} from '@material-ui/core/';
import RefreshIcon from '@material-ui/icons/Refresh';
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
        <TextField 
          InputProps={{
            classes: {
              notchedOutline: classes.notchedOutline
            }
          }} 
          id="outlined-separator" label="Separator" 
          variant="outlined"
          onChange={(evt) => setNameState(evt.target.value)}
        />
        <Button variant="contained" color="primary" onClick={() => console.log(`Gen keypair! ${nameState}`) }> <RefreshIcon /></Button>

        <TextField
          placeholder="Yeah! PassWords"
          multiline
          variant="outlined"
          rows={2}
          rowsMax={Infinity}
          fullWidth
          value="something"
          InputProps={{
            readOnly: true,
            classes: {
              notchedOutline: classes.notchedOutline
            }
          }}
        />
        <Button variant="contained" color="primary" onClick={() => console.log(`Show public key! ${nameState}`) }> <RefreshIcon /></Button>

        <TextField
          placeholder="Yeah! PassWords"
          multiline
          variant="outlined"
          rows={2}
          rowsMax={Infinity}
          fullWidth
          value="something"
          InputProps={{
            readOnly: true,
            classes: {
              notchedOutline: classes.notchedOutline
            }
          }}
        />
        <Button variant="contained" color="primary" onClick={() => console.log(`Show private key! ${nameState}`) }> <RefreshIcon /></Button>


    </div>
}

export default Keypair
