import React from 'react'
import { useForm } from "react-hook-form";
import { Slider } from '@material-ui/core';
import { ThemeProvider } from '@material-ui/core/styles';

import { createMuiTheme } from '@material-ui/core/styles';

const {register, handleSubmit, errors} = useForm;

// if material ui is good create a constants folder and put the theme there
// + refine a good theme with currently picked colors: https://in-your-saas.github.io/material-ui-theme-editor/
// Now: learn theming :)
// Theming problem slider read all and understand! https://stackoverflow.com/questions/50831450/how-to-overwrite-classes-and-styles-in-material-ui-react
const theme = createMuiTheme({
  palette: {
    primary: {
      main: '#0088A9',
    },
    secondary: {
      main: '#edf0f1',
    }
  },
});


function valuetext(value) {
  return {value};

}
function Words() {

  return <div className="toolsframe">
      <a>Words tbd</a>
      <form>

      </form>
    
    <ThemeProvider theme={theme}>
      
      <div>
        Number of words:
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