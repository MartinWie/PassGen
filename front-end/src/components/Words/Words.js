import React from 'react'
import { useForm } from "react-hook-form";
import { Slider,FormLabel } from '@material-ui/core';


const {register, handleSubmit, errors} = useForm;

// if material ui is good create a constants folder and put the theme there
// + refine a good theme with currently picked colors: https://in-your-saas.github.io/material-ui-theme-editor/
// Now: learn theming :)
// Theming problem slider read all and understand! https://stackoverflow.com/questions/50831450/how-to-overwrite-classes-and-styles-in-material-ui-react



function valuetext(value) {
  return {value};

}
function Words() {

  return <div className="toolsframe">
      <a>Words tbd</a>
      <form>

      </form>
      
      <div className="">
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
        <FormLabel>Test label(move right to slider and display sliders value)</FormLabel>
      </div>
  </div>
}

export default Words;