import React,{useEffect, useState} from 'react'
import PassContainer from './PassContainer'

function SendGenerator(props) {
    const [sendGeneratorTypeState,setSendGeneratorTypeState] = useState("")
    
    useEffect(() => {
        setSendGeneratorTypeState(props.value)
    }, [props.value])

    return(
        <div>
            {props.type}  
            <PassContainer value="Link to share the credential" />
            {sendGeneratorTypeState != String ? <PassContainer value="Public key" /> : null}
        </div>
    )
}
 // fix logik to show hide public key field, then add input for name/pw
export default SendGenerator