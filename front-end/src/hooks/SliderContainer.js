import React, {useState} from 'react'
import {Button, TextField, Checkbox, FormControlLabel, Slider} from '@material-ui/core/';
import {isMobile} from "react-device-detect";


const useStyles = makeStyles({
  notchedOutline: {borderColor: theme.palette.primary.main + " !important"},
});

function SliderContainer(props) {

  const [sliderState, setSliderState] = useState(4)

  return <div>
      <Slider
        defaultValue={props.defaultValue}
        aria-labelledby="discrete-slider-small-steps"
        step={1}
        marks
        min={props.min}
        max={props.max}
        valueLabelDisplay="auto"
        onChange={(evt, value) => setSliderState(value)}
    />
      
  </div>

  // remove state and pass ind props (including handling function(lookup how this works with react))
  // add texfield/text that shows the current number + regen button

}

export default SliderContainer;