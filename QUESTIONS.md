# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```
I would definitely refactor the database access layer and manipulation.
I see the implementation and database access are in a scattered way like, some places we do direct queries and in some, we use the repository pattern.
I would refactor the code to use the repository pattern for all database access and queries.
``

```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```
I personally prefer the code to be under the developer control, So I found the warehouse implementation part bit tricky and hard to follow. But still when I came to know about this(This is very new for me), this standardizes the API design. 
Still not suitable for small projects. But for larger projects, this is a good approach. 
I would use the OpenAPI specification to generate the API endpoints and then write the tests for those endpoints. 
This way, I can ensure that the API endpoints are working as expected and that the tests are comprehensive.

```
----
3. Given that you have limited time and resources for implementing tests for this project, what would be your approach/plan implementing those? Why?

**Answer:**
```
I would concentrate more on the Business logic and how well I can use the testcases to only those logics to validate whether the requirements are in order. And that is how I implemented this current assessment also.

```