import { useState} from "react";

const Todo = () => {
    let [todoText, setTodoText] = useState("");
    let [todos, setTodos] = useState([]);

    function handleTodoTextChange(e) {
        setTodoText(e.target.value);
    }

    function handleSaveClick() {
        fetch(process.env.REACT_APP_TODO_ULR, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ "text": todoText })
        }).then(response => response.json())
            .then(todo => setTodos([...todos, todo]))
    }

    return (
        <>
            <h1>Todo App</h1>
            <input type="text" value={todoText} onChange={handleTodoTextChange} />
            <input type="button" value="Save!" onClick={handleSaveClick} />
            <p>
                {todos.map(todo => (
                    <div key={todo.id}>{todo.id} {todo.text}</div>
                ))}
            </p>
        </>
    );
}

export default Todo;