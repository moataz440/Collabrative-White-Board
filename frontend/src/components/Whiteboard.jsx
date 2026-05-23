import React, { useRef, useEffect, useState } from 'react';
import io from 'socket.io-client';

const socket = io('http://localhost:3001');

const Whiteboard = () => {
    const canvasRef = useRef(null);
    const [isDrawing, setIsDrawing] = useState(false);
    const [color, setColor] = useState('#000000');
    const [lineWidth, setLineWidth] = useState(5);

    useEffect(() => {
        const canvas = canvasRef.current;
        const ctx = canvas.getContext('2d');

        // Set canvas size
        canvas.width = 800;
        canvas.height = 600;

        // Handle incoming draw events
        socket.on('draw', ({ x, y, color, lineWidth, type }) => {
            ctx.lineWidth = lineWidth;
            ctx.lineCap = 'round';
            ctx.strokeStyle = color;

            if (type === 'start') {
                ctx.beginPath();
                ctx.moveTo(x, y);
            } else if (type === 'draw') {
                ctx.lineTo(x, y);
                ctx.stroke();
            } else if (type === 'end') {
                ctx.closePath();
            }
        });

        return () => {
            socket.off('draw');
        };
    }, []);

    const startDrawing = ({ nativeEvent }) => {
        const { offsetX, offsetY } = nativeEvent;
        setIsDrawing(true);
        const ctx = canvasRef.current.getContext('2d');
        ctx.beginPath();
        ctx.moveTo(offsetX, offsetY);
        ctx.lineWidth = lineWidth;
        ctx.lineCap = 'round';
        ctx.strokeStyle = color;

        socket.emit('draw', { x: offsetX, y: offsetY, color, lineWidth, type: 'start' });
    };

    const draw = ({ nativeEvent }) => {
        if (!isDrawing) return;
        const { offsetX, offsetY } = nativeEvent;
        const ctx = canvasRef.current.getContext('2d');
        ctx.lineTo(offsetX, offsetY);
        ctx.stroke();

        socket.emit('draw', { x: offsetX, y: offsetY, color, lineWidth, type: 'draw' });
    };

    const stopDrawing = () => {
        if (!isDrawing) return;
        setIsDrawing(false);
        const ctx = canvasRef.current.getContext('2d');
        ctx.closePath();
        socket.emit('draw', { type: 'end' });
    };

    return (
        <div className="whiteboard-container">
            <div className="controls">
                <label>Color: </label>
                <input
                    type="color"
                    value={color}
                    onChange={(e) => setColor(e.target.value)}
                />
                <label> Size: </label>
                <input
                    type="range"
                    min="1"
                    max="20"
                    value={lineWidth}
                    onChange={(e) => setLineWidth(e.target.value)}
                />
                <span>{lineWidth}px</span>
            </div>
            <canvas
                ref={canvasRef}
                onMouseDown={startDrawing}
                onMouseMove={draw}
                onMouseUp={stopDrawing}
                onMouseLeave={stopDrawing}
                style={{ border: '2px solid #333', background: '#fff', borderRadius: '8px', cursor: 'crosshair' }}
            />
        </div>
    );
};

export default Whiteboard;
