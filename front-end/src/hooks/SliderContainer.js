import React from 'react'
import {Slider} from '@material-ui/core/';
import {isMobile} from "react-device-detect";
import InputContainer from './InputContainer'
import './SliderContainer.css'

function SliderContainer(props) {

  return <div style={wordsInputStylingPerDevice()}>
    <div className="slidercontainer__slider">
      <Slider
        defaultValue={props.defaultValue ? props.defaultValue : 0}
        aria-labelledby="discrete-slider-small-steps"
        step={1}
        marks
        min={props.min}
        max={props.max}
        valueLabelDisplay="auto"
        onChangeCommitted={(evt, value) => props.setSliderValue(value)}
      />
    </div>
    <div className="slidercontainer__inputs">
      { props.hideSeperatorInput == null &&
        <InputContainer outlinedName="Separator" inputChange={props.setSeperatorState} hideRefreshbutton hideDownloadbutton/>
      }
      <InputContainer hideDownloadbutton 
        outlinedName="Length" 
        inputChange={props.setSliderValue} 
        redoFunction={props.redoButtonFunction} 
        inputValue={props.sliderValue} 
      />
    </div>
      
  </div>

  function wordsInputStylingPerDevice(){
    var style = {
      display: 'flex'
    }
    if(isMobile){
      style = {
        display: 'grid'
      }
    }
    return style;
  }

}

export default SliderContainer;