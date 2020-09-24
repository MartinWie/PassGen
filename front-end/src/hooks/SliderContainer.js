import React from 'react'
import {Slider} from '@material-ui/core/';
import InputContainer from './InputContainer'

function SliderContainer(props) {

  return <div>
    <Slider
      defaultValue={props.defaultValue ? props.defaultValue : 0}
      aria-labelledby="discrete-slider-small-steps"
      step={1}
      marks
      min={props.min}
      max={props.max}
      valueLabelDisplay="auto"
      onChange={(evt, value) => props.setSliderValue(value)}
    />
    <InputContainer hideDownloadbutton outlinedName="Length" inputChange={props.setSliderValue} redoFunction={props.redoButtonFunction} inputValue={props.sliderValue} />
      
  </div>

  // remove state and pass ind props (including handling function(lookup how this works with react))
  // add texfield/text that shows the current number + regen button

}

export default SliderContainer;