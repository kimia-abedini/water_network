create endpoint that serves the HTML page.

for the simplicity at first just load the whole map like you did in the regular HTML.

then before rendering the HTML, load all the reports with Django ORM, and pass all the objects, through the context of the render() function. 

then go ahead in the HTML file, and in the script section, try to add points in JS part of your script to the map.

this way, you can show all the reports + map.


PS:
- for passing the context you can use something like https://stackoverflow.com/a/61740197
- for having the filters you can pass them throughout the URL, this way you can easily pass arguments to the Django orm query.