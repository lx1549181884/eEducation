import React, { useRef, useEffect } from 'react';
import Message from './message';
import { Input } from '@material-ui/core';
import Button from '../custom-button';
import './panel.scss';
import { List } from 'immutable';
import { ChatMessage } from '../../reducers/types';
import { useRootContext } from '../../store';
import useChatControl from '../../hooks/use-chat-control';

interface ChatPanelProps {
  messages: List<ChatMessage>
  value: string
  sendMessage: (evt: any) => void
  handleChange: (evt: any) => void
}

const regexPattern = /^\s+/;

const truncateBlank: (m: string) => string = (message: string) => message.replace(regexPattern, '');

export default function ChatPanel ({
  messages,
  value,
  sendMessage,
  handleChange,
}: ChatPanelProps) {

  const {store} = useRootContext();

  const {handleMute, disableChat, muteControl, muteChat} = useChatControl();

  const ref = useRef(null);

  const scrollDown = (current: any) => {
    current.scrollTop = current.scrollHeight;
  }

  useEffect(() => {
    scrollDown(ref.current);
  }, [messages]);

  return (
    <>
      <div className="chat-messages-container">
        <div className="chat-messages" ref={ref}>
          {messages.map((item: ChatMessage, key: number) => (
            <Message key={key} nickname={item.account} content={item.text} sender={item.id === store.user.id} />
          ))}
        </div>   
      </div>
      <div className="message-panel">
        {muteControl ?
          <div className={`icon ${muteChat ? 'icon-chat-off' : 'icon-chat-on' }`}
            onClick={() => {
              handleMute(muteChat ? 'unmute' : 'mute')
            }}></div> : null}
        <Input
          disabled={disableChat}
          value={value}
          placeholder="Input Message"
          disableUnderline
          className={"message-input"}
          onKeyPress={async (evt: any) => {
            if (evt.key === 'Enter') {
              const val = truncateBlank(value)
              val.length > 0 && await sendMessage(val);
            }
          }}
          onChange={handleChange}/>
        <Button className={'chat-panel-btn'} name={"send"}
          onClick={async (evt: any) => {
            const val = truncateBlank(value)
            val.length > 0 && await sendMessage(val);
          }} />
      </div>
    </>
  )
}