import React, {useState} from 'react'
import './Send.css'
import { makeStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';

const useStyles = makeStyles({
    root: {
      flexGrow: 1,
      backgroundColor: "transparent",
    },
    palette: {
        text: {
          primary: "#FFFFFF"
        }
    },
  });

function Send() {
    const classes = useStyles();
    const [value, setValue] = useState(0);

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };
    
    
    return <div className="toolsframe">
        <Paper className={classes.root}>
        <Tabs
            value={value}
            onChange={handleChange}
            indicatorColor="primary"
            textColor="primary"
            centered
        >
            <Tab label="Password" value="Test"/>
            <Tab label="Keypair" />
        </Tabs>
        </Paper>
    </div>
}
// rmove this and work with a simple radio button 
export default Send;