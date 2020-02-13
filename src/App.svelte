<script>
  import { AppBar, Card, Button, Image } from "smelte";

  const listProjects = [{
    heading: 'Google',
    subheading: 'Subheading 1',
    url: 'https://google.de',
    imageUrl: 'https://placekitten.com/300/200'
	}, {
    heading: 'Amazon',
    subheading: 'Subheading 2',
    url: 'https://amazon.de',
    imageUrl: 'https://placekitten.com/300/200'
	}, {
    heading: 'Amazon',
    subheading: 'Subheading 2',
    url: 'https://amazon.de',
    imageUrl: 'https://placekitten.com/300/200'
	},{
    heading: 'Amazon',
    subheading: 'Subheading 2',
    url: 'https://amazon.de',
    imageUrl: 'https://placekitten.com/300/200'
	}, {
    heading: 'Github',
    subheading: 'Subheading 3',
    url: 'https://github.com',
    imageUrl: 'https://placekitten.com/300/200'
  }];

  const listClubhead = [{
    firstname: 'Jonas',
    lastname: 'Wanke',
    email: 'jonas@wanke.de',
    imageUrl: 'https://placekitten.com/300/200'
  }, {
    firstname: 'Marcel',
    lastname: 'Garus',
    email: 'marcel@garus.de',
    imageUrl: 'https://placekitten.com/300/200'
  }];
  
</script>

<svelte:head>
  <title>HPI MobileDev-Klub</title>
</svelte:head>

<AppBar color="bg-hpi-red">
  <a href="." class="px-2 md:px-8">
    <h6 class="p-3 text-white tracking-widest font-thin text-lg">HPI MobileDev-Klub</h6>
  </a>
</AppBar>
<div class="container p-8 lg:max-w-5xl mx-auto h-full items-center mt-24">
  <section class="text-center">
    <h2>Willkommen auf der Startseite des HPI MobileDev-Klubs!</h2>
    <p class="py-4">Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</p>
  </section>
  <section>
    <h4 class="pb-3">Unsere Projekte</h4>
    <div class="flex flex-wrap">
      {#each listProjects as project}
      <div class="w-1/3 p-2">
        <a href="{ project.url }" target="_blank">
          <Card.Card>
            <div slot="media">
              <Image
                class="w-full"
                src="{ project.imageUrl }"
                alt="kitty"
              />
            </div>
            <div slot="text" class="p-5 pb-2 pt-3 text-gray-700 body-2">
              <h6>{ project.heading }</h6>
              <p>{ project.subheading }</p>
            </div>
          </Card.Card>
        </a>
      </div>
      {/each}
    </div>
  </section>
  <section>
    <h4 class="pb-3">Unsere Klubsprecher</h4>
    <div class="flex flex-wrap">
      {#each listClubhead as clubhead}
      <div class="w-1/3 p-2">
        <Card.Card hover={ false }>
          <div slot="media">
            <Image
              class="w-full"
              src="{ clubhead.imageUrl }"
              alt="kitty"
            />
          </div>
          <div slot="text" class="p-5 pb-2 pt-3 text-gray-700 body-2">
            <h6>{ clubhead.firstname + ' ' + clubhead.lastname}</h6>
            <a class="text-hpi-red" href="mailto:{ clubhead.email }">{ clubhead.email }</a>
          </div>
        </Card.Card>
      </div>
      {/each}
    </div>
  </section>
  <section>
    <h4 class="pb-3">Klubtreffen (Termine)</h4>
    <p>Unser Klub trifft sich in der Vorlesungszeit an jedem Donnerstag um 17 Uhr.</p>
  </section>
  
</div>
<div class="w-full bg-gray-200 h-12">
  <div class="container lg:max-w-5xl mx-auto">
    <div class="flex">
      <p class="pr-5">Impressum</p>
      <p>Datenschutz</p>
    </div>
  </div>
</div>