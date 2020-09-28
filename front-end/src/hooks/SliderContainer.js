import React from 'react'
import {Slider} from '@material-ui/core/';
import InputContainer from './InputContainer'
import './SliderContainer.css'

function SliderContainer(props) {

  return <div className="slidercontainer">
    <div className="slidercontainer__slider">
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
    </div>
    
    <InputContainer hideDownloadbutton outlinedName="Length" inputChange={props.setSliderValue} redoFunction={props.redoButtonFunction} inputValue={props.sliderValue} />
      
  </div>
}

export default SliderContainer;