import React from 'react'
import {Button, TextField} from '@material-ui/core/';
import RefreshIcon from '@material-ui/icons/Refresh';
import CloudDownloadIcon from '@material-ui/icons/CloudDownload';
import {makeStyles} from '@material-ui/core/styles'
import theme from '../config/theme';
import './InputContainer.css'

const useStyles = makeStyles({
    notchedOutline: {borderColor: theme.palette.primary.main + " !important"},
});

function InputContainer(props) {
    const classes = useStyles();

    return(
        <div className="naming-container--attributes">
            <TextField fullWidth
                InputProps={{
                    classes: {
                    notchedOutline: classes.notchedOutline
                    }
                }} 
                id="outlined-name" label={props.outlinedName ? props.outlinedName : "Name"} 
                variant="outlined"
                onChange={(evt) => props.inputChange(evt.target.value)}
                value={props.inputValue}
            />

            { props.hideDownloadbutton == null &&
                <Button id="keypairDownloadButton" 
                    variant="contained" 
                    color="primary" 
                    onClick={() => console.log(`Download keypair!`) }
                    >
                        <CloudDownloadIcon />
                </Button>
            }

            { props.hideRefreshbutton == null && 
                <Button variant="contained" 
                    color="primary" 
                    onClick={props.redoFunction}
                    > 
                    <RefreshIcon />
                </Button>
            }
      </div>
    )
    
}

export default InputContainer