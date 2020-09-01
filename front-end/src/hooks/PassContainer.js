import React, {useState} from 'react'
import VisibilityIcon from '@material-ui/icons/Visibility';
import AssignmentIcon from '@material-ui/icons/Assignment';
import './PassContainer.css'

function PassContainer() {

    return(
        <div className="passcontainer">
        Private key:
        <div className="passcontainer--block">
          <div className="passcontainer-text">
            <TextField
              placeholder="Yeah! PassWords"
              multiline
              variant="outlined"
              rows={2}
              fullWidth
              rowsMax={Infinity}
              value=""
              InputProps={{
                readOnly: true,
                classes: {
                  notchedOutline: classes.notchedOutline
                }
              }}
            />
          </div>
          <Button variant="contained" color="primary" onClick={() => console.log(`Show private key! ${nameState}`) }> <VisibilityIcon /></Button>
          <Button variant="contained" color="primary" onClick={() => console.log(`copy private key! ${nameState}`) }> <AssignmentIcon /></Button>
        </div>
      </div>
    )   
}

export default PassContainer