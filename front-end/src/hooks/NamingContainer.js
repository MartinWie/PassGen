import React,{useState} from 'react'
import {Button, TextField} from '@material-ui/core/';
import RefreshIcon from '@material-ui/icons/Refresh';
import CloudDownloadIcon from '@material-ui/icons/CloudDownload';
import {makeStyles} from '@material-ui/core/styles'
import theme from '../config/theme';
import './NamingContainer.css'

const useStyles = makeStyles({
    notchedOutline: {borderColor: theme.palette.primary.main + " !important"},
});

function NamingContainer(props) {
    const classes = useStyles();
    const [nameState,setNameState] = useState("key")

    return(
        <div className="naming-container--attributes">
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
            { props.hideDownloadbutton == null &&
                <Button id="keypairDownloadButton" 
                    variant="contained" 
                    color="primary" 
                    onClick={() => console.log(`Download keypair! ${nameState}`) }
                    >
                        <CloudDownloadIcon />
                </Button>
            }
            <Button variant="contained" 
                color="primary" 
                onClick={() => console.log(`Gen keypair! ${nameState}`) }
                > 
                <RefreshIcon />
            </Button>
      </div>
    )
    
}
// complete parameters than replace(Placeholder for keynbame/input and parameter for buttons) keypair css and jas input with this compononent
// rename Naming Container to InputContainer and add prop for Changing the label(default "Input")
// After that refactor main page and look for comments to improve and then start backend :)
export default NamingContainer