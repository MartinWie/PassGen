import React,{useEffect, useState} from 'react'
import PassContainer from './PassContainer'

function SendOutput(props) {
    const [sendGeneratorTypeState,setSendGeneratorTypeState] = useState(props.type)
    
    useEffect(() => {
        setSendGeneratorTypeState(props.type)
    }, [props.type])

    return(
        <div>
            <PassContainer title="Link to share the credential:" value="" />
            {sendGeneratorTypeState == "key" ? <PassContainer title="Public key:" value="" /> : null}
        </div>
    )
}

export default SendOutput