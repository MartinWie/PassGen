import React from 'react'
import PassContainer from './PassContainer'

function SendGenerator(props) {
    
    return(
        <div>
            {props.type}  
            <PassContainer value="Link to share the credential" />
            {props.value == "password" ? <PassContainer value="Public key" /> : null}
        </div>
    )
}
 // fix logik to show hide public key field, then add input for name/pw
export default SendGenerator