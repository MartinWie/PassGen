import React from 'react'
import { useForm } from "react-hook-form";
import { Slider } from '@material-ui/core';
import { makeStyles,ThemeProvider } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';

import { createMuiTheme } from '@material-ui/core/styles';

const {register, handleSubmit, errors} = useForm;
const useStyles = makeStyles({
  root: {
    width: 300,
  },
});

// if material ui is good create a constants folder and put the theme there
// + refine a good theme with currently picked colors: https://in-your-saas.github.io/material-ui-theme-editor/
// Now: learn theming :)
// Theming problem slider read all and understand! https://stackoverflow.com/questions/50831450/how-to-overwrite-classes-and-styles-in-material-ui-react
const theme = createMuiTheme({
  palette: {
    primary: {
      main: '#0088A9',
    },
  },
});


function valuetext(value) {
  return {value};

}
function Words() {
  const classes = useStyles();

  return <div className="toolsframe">
      <a>Words tbd</a>
      <form>
      </form>
    
    <ThemeProvider theme={theme}>
      
      <div className={classes.root}>
        <Typography id="discrete-slider-small-steps" gutterBottom>
          Number of words:
        </Typography>
        <Slider
          defaultValue={4}
          getAriaValueText={valuetext}
          aria-labelledby="discrete-slider-small-steps"
          step={1}
          marks
          min={1}
          max={8}
          valueLabelDisplay="auto"
        />
      </div>
    </ThemeProvider>


  </div>
}

export default Words;