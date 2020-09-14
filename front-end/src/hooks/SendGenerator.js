import React,{useEffect, useState} from 'react'
import PassContainer from './PassContainer'

function SendGenerator(props) {
    const [sendGeneratorTypeState,setSendGeneratorTypeState] = useState(props.type)
    
    useEffect(() => {
        setSendGeneratorTypeState(props.type)
    }, [props.type])

    return(
        <div>
            <PassContainer value="Link to share the credential" />
            {sendGeneratorTypeState == "key" ? <PassContainer value="Public key" /> : null}
        </div>
    )
}

export default SendGenerator