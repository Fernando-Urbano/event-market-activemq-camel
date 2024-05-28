```mermaid
sequenceDiagram
    participant Producer as Producer
    participant Filter as Message Filter
    participant Consumer as Consumer
    
    Producer->>Filter: Message
    Note right of Filter: Validate Message Content
    alt valid message
        Filter->>Consumer: Forwarded Message
    else invalid message
        Note right of Filter: Drop Message
    end
```